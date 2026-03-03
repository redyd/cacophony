package com.mapatiri.node.service;

import com.mapatiri.node.model.BcryptParams;

public interface BcryptService {
    /**
     * Permet d'extraire les param lors du challenge pour un user apres avoir utilisé getUser
     *
     * @param byCryptString
     * @return
     */
    BcryptParams parseByCrypt(String byCryptString);
}
