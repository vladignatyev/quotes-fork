package com.quote.mosaic.data.model.hints

import com.fasterxml.jackson.annotation.JsonProperty

data class HintsVariantsDO(
    @JsonProperty("AUTHOR")
    val author: List<HintDO>,
    @JsonProperty("NEXT_WORD")
    val nextWord: List<HintDO>,
    @JsonProperty("SKIP_LEVEL")
    val skipLevel: List<HintDO>
)