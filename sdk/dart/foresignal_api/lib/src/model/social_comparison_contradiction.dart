//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_comparison_contradiction.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialComparisonContradiction {
  /// Returns a new [SocialComparisonContradiction] instance.
  SocialComparisonContradiction({

    required  this.reason,

    required  this.ref,
  });

  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonContradictionReasonEnum reason;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialComparisonContradiction &&
      other.reason == reason &&
      other.ref == ref;

    @override
    int get hashCode =>
        reason.hashCode +
        ref.hashCode;

  factory SocialComparisonContradiction.fromJson(Map<String, dynamic> json) => _$SocialComparisonContradictionFromJson(json);

  Map<String, dynamic> toJson() => _$SocialComparisonContradictionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialComparisonContradictionReasonEnum {
@JsonValue(r'negating-record')
negatingRecord(r'negating-record'),
@JsonValue(r'media-reuse')
mediaReuse(r'media-reuse');

const SocialComparisonContradictionReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


