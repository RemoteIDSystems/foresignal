//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_context_anchor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertContextAnchor {
  /// Returns a new [AlertContextAnchor] instance.
  AlertContextAnchor({

     this.source_,

     this.externalId,

     this.seq,

     this.distanceKm,
  });

  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertContextAnchor &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.seq == seq &&
      other.distanceKm == distanceKm;

    @override
    int get hashCode =>
        source_.hashCode +
        externalId.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        (distanceKm == null ? 0 : distanceKm.hashCode);

  factory AlertContextAnchor.fromJson(Map<String, dynamic> json) => _$AlertContextAnchorFromJson(json);

  Map<String, dynamic> toJson() => _$AlertContextAnchorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

