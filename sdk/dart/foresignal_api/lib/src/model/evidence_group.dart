//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'evidence_group.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EvidenceGroup {
  /// Returns a new [EvidenceGroup] instance.
  EvidenceGroup({

    required  this.id,

    required  this.canonicalEventId,

    required  this.originatorRoot,

    required  this.memberCount,

    required  this.firstSeq,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'canonicalEventId',
    required: true,
    includeIfNull: false,
  )


  final String canonicalEventId;



  @JsonKey(
    
    name: r'originatorRoot',
    required: true,
    includeIfNull: false,
  )


  final String originatorRoot;



  @JsonKey(
    
    name: r'memberCount',
    required: true,
    includeIfNull: false,
  )


  final int memberCount;



  @JsonKey(
    
    name: r'firstSeq',
    required: true,
    includeIfNull: false,
  )


  final int firstSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EvidenceGroup &&
      other.id == id &&
      other.canonicalEventId == canonicalEventId &&
      other.originatorRoot == originatorRoot &&
      other.memberCount == memberCount &&
      other.firstSeq == firstSeq;

    @override
    int get hashCode =>
        id.hashCode +
        canonicalEventId.hashCode +
        originatorRoot.hashCode +
        memberCount.hashCode +
        firstSeq.hashCode;

  factory EvidenceGroup.fromJson(Map<String, dynamic> json) => _$EvidenceGroupFromJson(json);

  Map<String, dynamic> toJson() => _$EvidenceGroupToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

