//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/digest_count_entities.dart';
import 'package:foresignal_api/src/model/digest_count_previous.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_count.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestCount {
  /// Returns a new [DigestCount] instance.
  DigestCount({

    required  this.sourceId,

    required  this.kind,

    required  this.new_,

    required  this.changed,

    required  this.removed,

     this.edges,

     this.routine,

     this.entities,

     this.byReason,

     this.previous,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



      /// FIRST + REAPPEAR versions
  @JsonKey(
    
    name: r'new',
    required: true,
    includeIfNull: false,
  )


  final int new_;



      /// SIGNIFICANT + CHANGE versions
  @JsonKey(
    
    name: r'changed',
    required: true,
    includeIfNull: false,
  )


  final int changed;



      /// TOMBSTONE versions
  @JsonKey(
    
    name: r'removed',
    required: true,
    includeIfNull: false,
  )


  final int removed;



      /// TRIGGER_EDGE versions
  @JsonKey(
    
    name: r'edges',
    required: false,
    includeIfNull: false,
  )


  final int? edges;



      /// SAMPLE + KEYFRAME + HEARTBEAT versions
  @JsonKey(
    
    name: r'routine',
    required: false,
    includeIfNull: false,
  )


  final int? routine;



  @JsonKey(
    
    name: r'entities',
    required: false,
    includeIfNull: false,
  )


  final DigestCountEntities? entities;



  @JsonKey(
    
    name: r'byReason',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byReason;



  @JsonKey(
    
    name: r'previous',
    required: false,
    includeIfNull: false,
  )


  final DigestCountPrevious? previous;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestCount &&
      other.sourceId == sourceId &&
      other.kind == kind &&
      other.new_ == new_ &&
      other.changed == changed &&
      other.removed == removed &&
      other.edges == edges &&
      other.routine == routine &&
      other.entities == entities &&
      other.byReason == byReason &&
      other.previous == previous;

    @override
    int get hashCode =>
        sourceId.hashCode +
        kind.hashCode +
        new_.hashCode +
        changed.hashCode +
        removed.hashCode +
        edges.hashCode +
        routine.hashCode +
        entities.hashCode +
        byReason.hashCode +
        (previous == null ? 0 : previous.hashCode);

  factory DigestCount.fromJson(Map<String, dynamic> json) => _$DigestCountFromJson(json);

  Map<String, dynamic> toJson() => _$DigestCountToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

