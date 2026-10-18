//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'import_report.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ImportReport {
  /// Returns a new [ImportReport] instance.
  ImportReport({

    required  this.sourcesApplied,

    required  this.sourcesUnknown,

     this.triggersUpserted,

     this.channelsUpserted,

     this.briefingsUpserted,

     this.customSourcesUpserted,

     this.llmProvidersUpserted,

     this.warnings,

     this.placesUpserted,

     this.viewsUpserted,

     this.watchlistsUpserted,

     this.pushProvidersUpserted,
  });

  @JsonKey(
    
    name: r'sourcesApplied',
    required: true,
    includeIfNull: false,
  )


  final List<String> sourcesApplied;



  @JsonKey(
    
    name: r'sourcesUnknown',
    required: true,
    includeIfNull: false,
  )


  final List<String> sourcesUnknown;



  @JsonKey(
    
    name: r'triggersUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? triggersUpserted;



  @JsonKey(
    
    name: r'channelsUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? channelsUpserted;



  @JsonKey(
    
    name: r'briefingsUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? briefingsUpserted;



  @JsonKey(
    
    name: r'customSourcesUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? customSourcesUpserted;



  @JsonKey(
    
    name: r'llmProvidersUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? llmProvidersUpserted;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



  @JsonKey(
    
    name: r'placesUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? placesUpserted;



  @JsonKey(
    
    name: r'viewsUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? viewsUpserted;



  @JsonKey(
    
    name: r'watchlistsUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? watchlistsUpserted;



  @JsonKey(
    
    name: r'pushProvidersUpserted',
    required: false,
    includeIfNull: false,
  )


  final int? pushProvidersUpserted;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ImportReport &&
      other.sourcesApplied == sourcesApplied &&
      other.sourcesUnknown == sourcesUnknown &&
      other.triggersUpserted == triggersUpserted &&
      other.channelsUpserted == channelsUpserted &&
      other.briefingsUpserted == briefingsUpserted &&
      other.customSourcesUpserted == customSourcesUpserted &&
      other.llmProvidersUpserted == llmProvidersUpserted &&
      other.warnings == warnings &&
      other.placesUpserted == placesUpserted &&
      other.viewsUpserted == viewsUpserted &&
      other.watchlistsUpserted == watchlistsUpserted &&
      other.pushProvidersUpserted == pushProvidersUpserted;

    @override
    int get hashCode =>
        sourcesApplied.hashCode +
        sourcesUnknown.hashCode +
        triggersUpserted.hashCode +
        channelsUpserted.hashCode +
        briefingsUpserted.hashCode +
        customSourcesUpserted.hashCode +
        llmProvidersUpserted.hashCode +
        warnings.hashCode +
        placesUpserted.hashCode +
        viewsUpserted.hashCode +
        watchlistsUpserted.hashCode +
        pushProvidersUpserted.hashCode;

  factory ImportReport.fromJson(Map<String, dynamic> json) => _$ImportReportFromJson(json);

  Map<String, dynamic> toJson() => _$ImportReportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

