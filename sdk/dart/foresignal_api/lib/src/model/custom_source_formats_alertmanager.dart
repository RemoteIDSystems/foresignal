//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_alertmanager.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsAlertmanager {
  /// Returns a new [CustomSourceFormatsAlertmanager] instance.
  CustomSourceFormatsAlertmanager({

     this.kindLabel,

     this.resolved,

     this.receiverPrefix = false,

     this.resolvedValidFor,

     this.station,
  });

      /// Label whose value becomes the kind; else the source's single kind
  @JsonKey(
    
    name: r'kindLabel',
    required: false,
    includeIfNull: false,
  )


  final String? kindLabel;



  @JsonKey(
    
    name: r'resolved',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsAlertmanagerResolvedEnum? resolved;



      /// externalId `{receiver}:{fingerprint}` instead of the fingerprint (059, alertmanager preset)
  @JsonKey(
    defaultValue: false,
    name: r'receiverPrefix',
    required: false,
    includeIfNull: false,
  )


  final bool? receiverPrefix;



      /// With resolved=record: validUntil = endsAt + this duration, e.g. `1h` (059)
  @JsonKey(
    
    name: r'resolvedValidFor',
    required: false,
    includeIfNull: false,
  )


  final String? resolvedValidFor;



      /// [lon, lat] used as the geometry of alerts without location labels (059)
  @JsonKey(
    
    name: r'station',
    required: false,
    includeIfNull: false,
  )


  final List<num>? station;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsAlertmanager &&
      other.kindLabel == kindLabel &&
      other.resolved == resolved &&
      other.receiverPrefix == receiverPrefix &&
      other.resolvedValidFor == resolvedValidFor &&
      other.station == station;

    @override
    int get hashCode =>
        (kindLabel == null ? 0 : kindLabel.hashCode) +
        resolved.hashCode +
        receiverPrefix.hashCode +
        (resolvedValidFor == null ? 0 : resolvedValidFor.hashCode) +
        (station == null ? 0 : station.hashCode);

  factory CustomSourceFormatsAlertmanager.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsAlertmanagerFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsAlertmanagerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CustomSourceFormatsAlertmanagerResolvedEnum {
@JsonValue(r'tombstone')
tombstone(r'tombstone'),
@JsonValue(r'record')
record(r'record');

const CustomSourceFormatsAlertmanagerResolvedEnum(this.value);

final String value;

@override
String toString() => value;
}


