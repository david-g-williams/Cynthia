package io.cynthia.lambdas;

import io.cynthia.core.model.LabelScore;
import io.cynthia.core.model.Lambda;
import io.cynthia.core.model.Model;
import io.cynthia.server.request.Request;

import java.util.List;

public class LogicGates implements Lambda {

    @Override
    public List<List<LabelScore>> process(final Request request, final Model model) {
        return null;
    }
}
