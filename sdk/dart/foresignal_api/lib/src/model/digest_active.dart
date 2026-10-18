//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_active.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestActive {
  /// Returns a new [DigestActive] instance.
  DigestActive({

    required  this.sourceId,

    required  this.kind,

    required  this.entities,

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



  @JsonKey(
    
    name: r'entities',
    required: true,
    includeIfNull: false,
  )


  final int entities;



      /// The previous period's count; null (record_live keeps only the latest receipt, so it is not known until 038's rollups)
  @JsonKey(
    
    name: r'previous',
    required: false,
    includeIfNull: false,
  )


  final int? previous;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestActive &&
      other.sourceId == sourceId &&
      other.kind == kind &&
      other.entities == entities &&
      other.previous == previous;

    @override
    int get hashCode =>
        sourceId.hashCode +
        kind.hashCode +
        entities.hashCode +
        (previous == null ? 0 : previous.hashCode);

  factory DigestActive.fromJson(Map<String, dynamic> json) => _$DigestActiveFromJson(json);

  Map<String, dynamic> toJson() => _$DigestActiveToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

