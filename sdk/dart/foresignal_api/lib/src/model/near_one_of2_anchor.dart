//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'near_one_of2_anchor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NearOneOf2Anchor {
  /// Returns a new [NearOneOf2Anchor] instance.
  NearOneOf2Anchor({

    required  this.source_,

    required  this.externalId,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NearOneOf2Anchor &&
      other.source_ == source_ &&
      other.externalId == externalId;

    @override
    int get hashCode =>
        source_.hashCode +
        externalId.hashCode;

  factory NearOneOf2Anchor.fromJson(Map<String, dynamic> json) => _$NearOneOf2AnchorFromJson(json);

  Map<String, dynamic> toJson() => _$NearOneOf2AnchorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

