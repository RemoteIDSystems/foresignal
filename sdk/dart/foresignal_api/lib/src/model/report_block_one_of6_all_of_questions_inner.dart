//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of6_all_of_questions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf6AllOfQuestionsInner {
  /// Returns a new [ReportBlockOneOf6AllOfQuestionsInner] instance.
  ReportBlockOneOf6AllOfQuestionsInner({

    required  this.questionRef,

     this.snapshot,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'questionRef',
    required: true,
    includeIfNull: false,
  )


  final String questionRef;



  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf6AllOfQuestionsInner &&
      other.questionRef == questionRef &&
      other.snapshot == snapshot;

    @override
    int get hashCode =>
        questionRef.hashCode +
        snapshot.hashCode;

  factory ReportBlockOneOf6AllOfQuestionsInner.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf6AllOfQuestionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf6AllOfQuestionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

