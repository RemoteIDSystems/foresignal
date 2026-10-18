//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_draft_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetDraftRequest {
  /// Returns a new [HypothesisSetDraftRequest] instance.
  HypothesisSetDraftRequest({

    required  this.text,

     this.situationId,

     this.regionCodes,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetDraftRequest &&
      other.text == text &&
      other.situationId == situationId &&
      other.regionCodes == regionCodes;

    @override
    int get hashCode =>
        text.hashCode +
        (situationId == null ? 0 : situationId.hashCode) +
        regionCodes.hashCode;

  factory HypothesisSetDraftRequest.fromJson(Map<String, dynamic> json) => _$HypothesisSetDraftRequestFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetDraftRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

