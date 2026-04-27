package es.upm.api.adapter.out.legal.mongo.customerfiledownload;

import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.criteria.CustomerFileDownloadFindCriteria;
import es.upm.api.domain.ports.out.legal.CustomerFileDownloadGateway;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class XxxAdapter implements XxxGateway {

    private final XxxRepository xxxRepository;

}
