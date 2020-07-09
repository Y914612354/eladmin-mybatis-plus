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
package com.admin.tools.service;

import com.admin.tools.model.AlipayConfigModel;
import com.admin.vo.TradeVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author adyfang
 * @date 2020年5月4日
 */
public interface IAliPayService extends IService<AlipayConfigModel> {

    String toPayAsPc(AlipayConfigModel alipay, TradeVo trade) throws Exception;

    String toPayAsWeb(AlipayConfigModel alipay, TradeVo trade) throws Exception;

    AlipayConfigModel find();

    /**
     * @param alipayConfig
     * @return
     */
    AlipayConfigModel config(AlipayConfigModel alipayConfig);

}
