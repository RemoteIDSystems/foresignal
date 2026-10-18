//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_subject.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolSubject {
  /// Returns a new [PolSubject] instance.
  PolSubject({

    required  this.id,

    required  this.subjectType,

    required  this.ref,

    required  this.enabled,

     this.params,

     this.private,

     this.createdBy,

    required  this.createdAt,

     this.lastSeq,

     this.lastComputedAt,

     this.placeCount,

     this.stayCount,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'subjectType',
    required: true,
    includeIfNull: false,
  )


  final PolSubjectSubjectTypeEnum subjectType;



  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



      /// Outputs inherit private and the precision cap from a private source
  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastSeq;



  @JsonKey(
    
    name: r'lastComputedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastComputedAt;



  @JsonKey(
    
    name: r'placeCount',
    required: false,
    includeIfNull: false,
  )


  final int? placeCount;



  @JsonKey(
    
    name: r'stayCount',
    required: false,
    includeIfNull: false,
  )


  final int? stayCount;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolSubject &&
      other.id == id &&
      other.subjectType == subjectType &&
      other.ref == ref &&
      other.enabled == enabled &&
      other.params == params &&
      other.private == private &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.lastSeq == lastSeq &&
      other.lastComputedAt == lastComputedAt &&
      other.placeCount == placeCount &&
      other.stayCount == stayCount;

    @override
    int get hashCode =>
        id.hashCode +
        subjectType.hashCode +
        ref.hashCode +
        enabled.hashCode +
        params.hashCode +
        private.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        (lastSeq == null ? 0 : lastSeq.hashCode) +
        (lastComputedAt == null ? 0 : lastComputedAt.hashCode) +
        placeCount.hashCode +
        stayCount.hashCode;

  factory PolSubject.fromJson(Map<String, dynamic> json) => _$PolSubjectFromJson(json);

  Map<String, dynamic> toJson() => _$PolSubjectToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PolSubjectSubjectTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'entity')
entity(r'entity'),
@JsonValue(r'watchlist')
watchlist(r'watchlist');

const PolSubjectSubjectTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


