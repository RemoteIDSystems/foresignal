//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag_assign_result_skipped_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TagAssignResultSkippedInner {
  /// Returns a new [TagAssignResultSkippedInner] instance.
  TagAssignResultSkippedInner({

    required  this.ref,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final TagAssignResultSkippedInnerReasonEnum reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TagAssignResultSkippedInner &&
      other.ref == ref &&
      other.reason == reason;

    @override
    int get hashCode =>
        ref.hashCode +
        reason.hashCode;

  factory TagAssignResultSkippedInner.fromJson(Map<String, dynamic> json) => _$TagAssignResultSkippedInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TagAssignResultSkippedInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TagAssignResultSkippedInnerReasonEnum {
@JsonValue(r'not-visible')
notVisible(r'not-visible'),
@JsonValue(r'invalid-ref')
invalidRef(r'invalid-ref');

const TagAssignResultSkippedInnerReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


