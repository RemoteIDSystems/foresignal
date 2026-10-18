//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDeviceInput {
  /// Returns a new [IngestDeviceInput] instance.
  IngestDeviceInput({

    required  this.externalId,

    required  this.name,
  });

      /// Forced externalId for everything this key ingests
  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDeviceInput &&
      other.externalId == externalId &&
      other.name == name;

    @override
    int get hashCode =>
        externalId.hashCode +
        name.hashCode;

  factory IngestDeviceInput.fromJson(Map<String, dynamic> json) => _$IngestDeviceInputFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

