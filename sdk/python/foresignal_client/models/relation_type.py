from enum import StrEnum


class RelationType(StrEnum):
    ABOUT = "about"
    AFFECTS = "affects"
    CAUSES = "causes"
    DEPENDS_ON = "depends-on"
    DERIVED_FROM = "derived-from"
    EVIDENCE_AGAINST = "evidence-against"
    EVIDENCE_FOR = "evidence-for"
    LOCATED_IN = "located-in"
    MEMBER_OF = "member-of"
    MENTIONS = "mentions"
    NEAR = "near"
    OBSERVED_BY = "observed-by"
    PRECEDES = "precedes"
    RELATED_TO = "related-to"
    SAME_AS = "same-as"

    def __str__(self) -> str:
        return str(self.value)
