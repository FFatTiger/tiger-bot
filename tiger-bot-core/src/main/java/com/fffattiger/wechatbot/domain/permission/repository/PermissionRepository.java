package com.fffattiger.wechatbot.domain.permission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fffattiger.wechatbot.domain.permission.Permission;
import com.fffattiger.wechatbot.domain.permission.PermissionType;

/**
 * 权限仓储接口
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限类型和上下文查找权限
     */
    List<Permission> findByTypeAndContextId(PermissionType type, String contextId);

    /**
     * 根据权限类型、资源和上下文查找权限
     */
    List<Permission> findByTypeAndResourceIdAndContextId(PermissionType type, String resourceId, String contextId);

    /**
     * 根据权限类型和上下文查找所有权限（包括已撤销的）
     */
    @Query("SELECT p FROM Permission p WHERE p.type = :type AND p.contextId = :contextId")
    List<Permission> findAllByTypeAndContextId(@Param("type") PermissionType type, @Param("contextId") String contextId);

    /**
     * 删除指定上下文的所有权限
     */
    void deleteByContextId(String contextId);

    /**
     * 删除指定类型和上下文的权限
     */
    void deleteByTypeAndContextId(PermissionType type, String contextId);

    /**
     * 检查特定权限是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.type = :type AND p.resourceId = :resourceId AND p.subjectId = :subjectId AND p.contextId = :contextId AND p.granted = true")
    boolean existsGrantedPermission(@Param("type") PermissionType type, 
                                   @Param("resourceId") String resourceId, 
                                   @Param("subjectId") String subjectId, 
                                   @Param("contextId") String contextId);

    /**
     * 查找用户在特定上下文中的所有权限
     */
    @Query("SELECT p FROM Permission p WHERE p.subjectId = :subjectId AND p.contextId = :contextId AND p.granted = true")
    List<Permission> findUserPermissionsInContext(@Param("subjectId") String subjectId, @Param("contextId") String contextId);

    /**
     * 查找指定上下文中的全局权限
     */
    @Query("SELECT p FROM Permission p WHERE p.subjectId IS NULL AND p.contextId = :contextId AND p.granted = true")
    List<Permission> findGlobalPermissionsInContext(@Param("contextId") String contextId);
} 