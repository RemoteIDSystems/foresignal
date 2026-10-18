//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_export_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleExportRequest {
  /// Returns a new [ChronicleExportRequest] instance.
  ChronicleExportRequest({

    required  this.format,

     this.layers,

     this.from,

     this.to,
  });

  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final ChronicleExportRequestFormatEnum format;



      /// Empty = all layers
  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleExportRequest &&
      other.format == format &&
      other.layers == layers &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        format.hashCode +
        layers.hashCode +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode);

  factory ChronicleExportRequest.fromJson(Map<String, dynamic> json) => _$ChronicleExportRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleExportRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleExportRequestFormatEnum {
@JsonValue(r'geoparquet')
geoparquet(r'geoparquet'),
@JsonValue(r'gpkg')
gpkg(r'gpkg'),
@JsonValue(r'json-bundle')
jsonBundle(r'json-bundle');

const ChronicleExportRequestFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


