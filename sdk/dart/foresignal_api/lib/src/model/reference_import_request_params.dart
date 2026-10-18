//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_import_request_params.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceImportRequestParams {
  /// Returns a new [ReferenceImportRequestParams] instance.
  ReferenceImportRequestParams({

     this.file,

     this.useApi,

     this.yearFrom,

     this.yearTo,

     this.countries,

     this.minMagnitude = 4.5,

     this.hazardTypes,

     this.version,
  });

      /// ucdp-ged: CSV (or the downloaded ZIP holding it) path relative to `${FORESIGNAL_DATA_DIR}/imports`
  @JsonKey(
    
    name: r'file',
    required: false,
    includeIfNull: false,
  )


  final String? file;



      /// ucdp-ged: use the UCDP API instead of a file
  @JsonKey(
    
    name: r'useApi',
    required: false,
    includeIfNull: false,
  )


  final bool? useApi;



          // minimum: 1900
  @JsonKey(
    
    name: r'yearFrom',
    required: false,
    includeIfNull: false,
  )


  final int? yearFrom;



          // minimum: 1900
  @JsonKey(
    
    name: r'yearTo',
    required: false,
    includeIfNull: false,
  )


  final int? yearTo;



      /// acled: ISO country codes
  @JsonKey(
    
    name: r'countries',
    required: false,
    includeIfNull: false,
  )


  final List<String>? countries;



      /// usgs-comcat: minimum magnitude
  @JsonKey(
    defaultValue: 4.5,
    name: r'minMagnitude',
    required: false,
    includeIfNull: false,
  )


  final num? minMagnitude;



      /// gdacs: hazard types (default all)
  @JsonKey(
    
    name: r'hazardTypes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hazardTypes;



      /// ucdp-ged with useApi: the GED release to read, e.g. `26.1` (required with useApi)
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceImportRequestParams &&
      other.file == file &&
      other.useApi == useApi &&
      other.yearFrom == yearFrom &&
      other.yearTo == yearTo &&
      other.countries == countries &&
      other.minMagnitude == minMagnitude &&
      other.hazardTypes == hazardTypes &&
      other.version == version;

    @override
    int get hashCode =>
        file.hashCode +
        useApi.hashCode +
        yearFrom.hashCode +
        yearTo.hashCode +
        countries.hashCode +
        minMagnitude.hashCode +
        hazardTypes.hashCode +
        version.hashCode;

  factory ReferenceImportRequestParams.fromJson(Map<String, dynamic> json) => _$ReferenceImportRequestParamsFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceImportRequestParamsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

