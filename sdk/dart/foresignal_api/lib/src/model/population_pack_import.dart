//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/population_pack_import_cells.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'population_pack_import.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PopulationPackImport {
  /// Returns a new [PopulationPackImport] instance.
  PopulationPackImport({

    required  this.id,

     this.path,

    required  this.fileSha256,

    required  this.epoch,

    required  this.licence,

    required  this.attribution,

     this.cells,

    required  this.importedAt,

     this.durationMs,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;



  @JsonKey(
    
    name: r'fileSha256',
    required: true,
    includeIfNull: false,
  )


  final String fileSha256;



  @JsonKey(
    
    name: r'epoch',
    required: true,
    includeIfNull: false,
  )


  final int epoch;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final PopulationPackImportLicenceEnum licence;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final PopulationPackImportAttributionEnum attribution;



  @JsonKey(
    
    name: r'cells',
    required: false,
    includeIfNull: false,
  )


  final PopulationPackImportCells? cells;



  @JsonKey(
    
    name: r'importedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime importedAt;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PopulationPackImport &&
      other.id == id &&
      other.path == path &&
      other.fileSha256 == fileSha256 &&
      other.epoch == epoch &&
      other.licence == licence &&
      other.attribution == attribution &&
      other.cells == cells &&
      other.importedAt == importedAt &&
      other.durationMs == durationMs;

    @override
    int get hashCode =>
        id.hashCode +
        path.hashCode +
        fileSha256.hashCode +
        epoch.hashCode +
        licence.hashCode +
        attribution.hashCode +
        cells.hashCode +
        importedAt.hashCode +
        durationMs.hashCode;

  factory PopulationPackImport.fromJson(Map<String, dynamic> json) => _$PopulationPackImportFromJson(json);

  Map<String, dynamic> toJson() => _$PopulationPackImportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PopulationPackImportLicenceEnum {
@JsonValue(r'CC-BY-4.0')
cCBY4Period0(r'CC-BY-4.0');

const PopulationPackImportLicenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PopulationPackImportAttributionEnum {
@JsonValue(r'© European Union, GHSL')
europeanUnionCommaGHSL(r'© European Union, GHSL');

const PopulationPackImportAttributionEnum(this.value);

final String value;

@override
String toString() => value;
}


