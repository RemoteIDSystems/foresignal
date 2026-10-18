//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of5.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf5 {
  /// Returns a new [ReportBlockOneOf5] instance.
  ReportBlockOneOf5({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

    required  this.hypothesisSetRef,

     this.snapshot,

     this.revisionIds,
  });

      /// Server-assigned; comment on a block with `report:{id}#{blockId}`
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ReportBlockOneOf5TypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// `llm` blocks are labelled narrative from the optional 028 tool loop
  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final ReportBlockOneOf5TextOriginEnum? textOrigin;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'updatedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? updatedBy;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'hypothesisSetRef',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisSetRef;



  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;



  @JsonKey(
    
    name: r'revisionIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? revisionIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf5 &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.hypothesisSetRef == hypothesisSetRef &&
      other.snapshot == snapshot &&
      other.revisionIds == revisionIds;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        hypothesisSetRef.hashCode +
        snapshot.hashCode +
        revisionIds.hashCode;

  factory ReportBlockOneOf5.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf5FromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf5ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockOneOf5TypeEnum {
@JsonValue(r'hypotheses')
hypotheses(r'hypotheses');

const ReportBlockOneOf5TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labelled narrative from the optional 028 tool loop
enum ReportBlockOneOf5TextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockOneOf5TextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


