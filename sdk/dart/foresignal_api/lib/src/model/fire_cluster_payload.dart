//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'fire_cluster_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FireClusterPayload {
  /// Returns a new [FireClusterPayload] instance.
  FireClusterPayload({

    required  this.pixels,

     this.frpTotalMw,

     this.frpMaxMw,

     this.firstSeenAt,

    required  this.label,
  });

  @JsonKey(
    
    name: r'pixels',
    required: true,
    includeIfNull: false,
  )


  final int pixels;



  @JsonKey(
    
    name: r'frpTotalMw',
    required: false,
    includeIfNull: false,
  )


  final num? frpTotalMw;



  @JsonKey(
    
    name: r'frpMaxMw',
    required: false,
    includeIfNull: false,
  )


  final num? frpMaxMw;



  @JsonKey(
    
    name: r'firstSeenAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? firstSeenAt;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FireClusterPayload &&
      other.pixels == pixels &&
      other.frpTotalMw == frpTotalMw &&
      other.frpMaxMw == frpMaxMw &&
      other.firstSeenAt == firstSeenAt &&
      other.label == label;

    @override
    int get hashCode =>
        pixels.hashCode +
        frpTotalMw.hashCode +
        frpMaxMw.hashCode +
        firstSeenAt.hashCode +
        label.hashCode;

  factory FireClusterPayload.fromJson(Map<String, dynamic> json) => _$FireClusterPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$FireClusterPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

