/*
 *  Copyright 2019-2020 Fang Jin Biao
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.admin.config;

import com.admin.modules.system.service.dto.RoleSmallDto;
import com.admin.modules.system.service.dto.UserDto;
import com.admin.system.model.DeptModel;
import com.admin.system.service.IDeptService;
import com.admin.system.service.IRoleService;
import com.admin.system.service.IUserService;
import com.admin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author adyfang
 * @date 2020年4月29日
 */
@Configuration
@RequiredArgsConstructor
public class AdminDataScope {

    final String[] scopeType = {"全部", "本级", "自定义"};

    private final IUserService userService;
    private final IRoleService roleService;
    private final IDeptService deptService;

    public Set<Long> getDeptIds() {

        UserDto user = userService.findByName(SecurityUtils.getCurrentUsername());

        // 用于存储部门id
        Set<Long> deptIds = new HashSet<>();

        // 查询用户角色
        List<RoleSmallDto> roleSet = roleService.findByUsersId(user.getId());

        for (RoleSmallDto role : roleSet) {

            if (scopeType[0].equals(role.getDataScope())) {
                return new HashSet<>();
            }

            // 存储本级的数据权限
            if (scopeType[1].equals(role.getDataScope())) {
                deptIds.add(user.getDept().getId());
            }

            // 存储自定义的数据权限
            if (scopeType[2].equals(role.getDataScope())) {
                Set<DeptModel> depts = deptService.findByRoleId(role.getId());
                for (DeptModel dept : depts) {
                    deptIds.add(dept.getId());
                    List<DeptModel> deptChildren = deptService.findByPid(dept.getId());
                    if (deptChildren != null && deptChildren.size() != 0) {
                        deptIds.addAll(getDeptChildren(deptChildren));
                    }
                }
            }
        }
        return deptIds;
    }

    public List<Long> getDeptChildren(List<DeptModel> deptList) {
        List<Long> list = new ArrayList<>();
        deptList.forEach(dept -> {
            if (dept != null && dept.getEnabled()) {
                List<DeptModel> depts = deptService.findByPid(dept.getId());
                if (deptList.size() != 0) {
                    list.addAll(getDeptChildren(depts));
                }
                list.add(dept.getId());
            }
        });
        return list;
    }
}
