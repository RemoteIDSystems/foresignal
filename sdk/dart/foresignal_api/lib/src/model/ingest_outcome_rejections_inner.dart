//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_outcome_rejections_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestOutcomeRejectionsInner {
  /// Returns a new [IngestOutcomeRejectionsInner] instance.
  IngestOutcomeRejectionsInner({

     this.index,

     this.externalId,

     this.reason,
  });

  @JsonKey(
    
    name: r'index',
    required: false,
    includeIfNull: false,
  )


  final int? index;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



      /// Rejection reason code (normalizer, prompt 008): EXTERNAL_ID_INVALID, UNKNOWN_KIND, MISSING_OBSERVED_AT, OBSERVED_AT_TOO_OLD, PAYLOAD_TOO_LARGE, PAYLOAD_TOO_DEEP, PAYLOAD_TOO_MANY_KEYS, PAYLOAD_STRING_TOO_LONG, PAYLOAD_NON_FINITE_NUMBER, PAYLOAD_UNSUPPORTED_TYPE, TOO_MANY_OBSERVERS, GEOMETRY_TOO_MANY_VERTICES, GEOMETRY_WRONG_SRID, COORDINATES_OUT_OF_RANGE, QUALITY_INVALID, VALID_RANGE_INVERTED. Format rejections (024, 039): INVALID_ITEM, LOW_ACCURACY (accuracy worse than maxAccuracyMeters), INVALID_FIX (valid=false, missing or out-of-range coordinates), NULL_ISLAND (exact 0,0), ENCRYPTED_UNSUPPORTED (OwnTracks payload encryption). Later prompts may add codes.
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestOutcomeRejectionsInner &&
      other.index == index &&
      other.externalId == externalId &&
      other.reason == reason;

    @override
    int get hashCode =>
        index.hashCode +
        (externalId == null ? 0 : externalId.hashCode) +
        reason.hashCode;

  factory IngestOutcomeRejectionsInner.fromJson(Map<String, dynamic> json) => _$IngestOutcomeRejectionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IngestOutcomeRejectionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

