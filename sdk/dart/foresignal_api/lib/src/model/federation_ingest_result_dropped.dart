//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_ingest_result_dropped.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationIngestResultDropped {
  /// Returns a new [FederationIngestResultDropped] instance.
  FederationIngestResultDropped({

     this.loop,

     this.hops,

     this.dedup,

     this.licence,

     this.private,

     this.exercise,

     this.expired,

     this.selection,
  });

  @JsonKey(
    
    name: r'loop',
    required: false,
    includeIfNull: false,
  )


  final int? loop;



  @JsonKey(
    
    name: r'hops',
    required: false,
    includeIfNull: false,
  )


  final int? hops;



  @JsonKey(
    
    name: r'dedup',
    required: false,
    includeIfNull: false,
  )


  final int? dedup;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final int? licence;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final int? private;



  @JsonKey(
    
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final int? exercise;



  @JsonKey(
    
    name: r'expired',
    required: false,
    includeIfNull: false,
  )


  final int? expired;



      /// Outside the selection, malformed, or of a kind the target source does not declare
  @JsonKey(
    
    name: r'selection',
    required: false,
    includeIfNull: false,
  )


  final int? selection;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationIngestResultDropped &&
      other.loop == loop &&
      other.hops == hops &&
      other.dedup == dedup &&
      other.licence == licence &&
      other.private == private &&
      other.exercise == exercise &&
      other.expired == expired &&
      other.selection == selection;

    @override
    int get hashCode =>
        loop.hashCode +
        hops.hashCode +
        dedup.hashCode +
        licence.hashCode +
        private.hashCode +
        exercise.hashCode +
        expired.hashCode +
        selection.hashCode;

  factory FederationIngestResultDropped.fromJson(Map<String, dynamic> json) => _$FederationIngestResultDroppedFromJson(json);

  Map<String, dynamic> toJson() => _$FederationIngestResultDroppedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

