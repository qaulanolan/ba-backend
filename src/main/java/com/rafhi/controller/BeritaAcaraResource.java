package com.rafhi.controller;

import com.rafhi.dto.DynamicGenerateRequest;
import com.rafhi.dto.HistoryResponseDTO;
import com.rafhi.entity.AppUser;
import com.rafhi.entity.BeritaAcaraHistory;
import com.rafhi.entity.TemplatePlaceholder;
import com.rafhi.service.BeritaAcaraService;
import com.rafhi.service.TemplateService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
// import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
// import java.util.stream.Collectors;

import jakarta.persistence.EntityManager; 
// import io.quarkus.panache.common.Sort; 

@Path("/berita-acara")
@ApplicationScoped
@Authenticated
public class BeritaAcaraResource {

    @Inject SecurityIdentity securityIdentity;
    @Inject BeritaAcaraService beritaAcaraService;
    @Inject TemplateService templateService;
    @Inject EntityManager entityManager;
    
    @GET
    @Path("/templates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveTemplates() {
        return Response.ok(templateService.listAllActive()).build();
    }

    @GET
    @Path("/templates/{id}/form-structure")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFormStructure(@PathParam("id") Long id) {
        List<TemplatePlaceholder> placeholders = TemplatePlaceholder.list("template.id", id);
        if (placeholders.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(placeholders).build();
    }
    
    @POST
    @Path("/generate-dynamic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public Response generateDynamic(DynamicGenerateRequest request) throws Exception {
        String currentUsername = securityIdentity.getPrincipal().getName();
        AppUser user = AppUser.find("username", currentUsername).firstResult();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        byte[] docxBytes = beritaAcaraService.generateDocument(request.templateId, request.data);
        BeritaAcaraHistory history = beritaAcaraService.saveHistory(request.templateId, user, request.data, docxBytes);

        // Buat nama file yang deskriptif dari data yang ada di riwayat
        // Ganti spasi dengan underscore dan hapus karakter yang tidak valid
        String safeJenis = history.jenisBeritaAcara.replaceAll("[^a-zA-Z0-9.-]", " ");
        String safeJudul = history.judulPekerjaan.replaceAll("[^a-zA-Z0-9.-]", " ");
        String fileName = String.format("%s - %s.docx", safeJenis, safeJudul);

        ResponseBuilder response = Response.ok(new ByteArrayInputStream(docxBytes));
        // Set header dengan nama file yang sudah diformat
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\""); 
        response.header("X-History-ID", history.id);
        response.header("Access-Control-Expose-Headers", "Content-Disposition, X-History-ID"); // <-- PENTING!
        return response.build();
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistory() {
        String currentUsername = securityIdentity.getPrincipal().getName();
        // List<BeritaAcaraHistory> historyList = BeritaAcaraHistory.find("user.username", currentUsername).list();

        // 1. Buat string query JPQL yang benar
        String jpqlQuery = "SELECT new com.rafhi.dto.HistoryResponseDTO(" +
                           "h.id, h.nomorBA, h.jenisBeritaAcara, h.judulPekerjaan, h.generationTimestamp) " +
                           "FROM BeritaAcaraHistory h WHERE h.user.username = :username " +
                           "ORDER BY h.generationTimestamp DESC";

        // 2. Buat query menggunakan EntityManager
        List<HistoryResponseDTO> responseList = entityManager.createQuery(jpqlQuery, HistoryResponseDTO.class)
                .setParameter("username", currentUsername)
                .getResultList();

        return Response.ok(responseList).build();
    }

    @GET
    @Path("/history/{id}/file")
    @Produces("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @Transactional
    public Response getHistoryFile(@PathParam("id") Long id) {
        String currentUsername = securityIdentity.getPrincipal().getName();
        BeritaAcaraHistory history = BeritaAcaraHistory.find("id = ?1 and user.username = ?2", id, currentUsername).firstResult();
        if (history == null) return Response.status(Response.Status.NOT_FOUND).build();
        
        String safeJenis = history.jenisBeritaAcara.replaceAll("[^a-zA-Z0-9.-]", " ");
        String safeJudul = history.judulPekerjaan.replaceAll("[^a-zA-Z0-9.-]", " ");
        String fileName = String.format("%s - %s.docx", safeJenis, safeJudul);

        try {
            java.nio.file.Path path = Paths.get(history.filePath);
            if (!Files.exists(path)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ResponseBuilder response = Response.ok(Files.newInputStream(path));
            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.header("Access-Control-Expose-Headers", "Content-Disposition"); // <-- PENTING!
            return response.build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Gagal membaca file riwayat.").build();
        }
    }
}
