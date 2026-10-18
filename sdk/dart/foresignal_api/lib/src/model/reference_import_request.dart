//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/reference_import_request_params.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_import_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceImportRequest {
  /// Returns a new [ReferenceImportRequest] instance.
  ReferenceImportRequest({

    required  this.dataset,

     this.params,
  });

  @JsonKey(
    
    name: r'dataset',
    required: true,
    includeIfNull: false,
  )


  final ReferenceImportRequestDatasetEnum dataset;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final ReferenceImportRequestParams? params;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceImportRequest &&
      other.dataset == dataset &&
      other.params == params;

    @override
    int get hashCode =>
        dataset.hashCode +
        params.hashCode;

  factory ReferenceImportRequest.fromJson(Map<String, dynamic> json) => _$ReferenceImportRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceImportRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReferenceImportRequestDatasetEnum {
@JsonValue(r'ucdp-ged')
ucdpGed(r'ucdp-ged'),
@JsonValue(r'acled')
acled(r'acled'),
@JsonValue(r'usgs-comcat')
usgsComcat(r'usgs-comcat'),
@JsonValue(r'gdacs')
gdacs(r'gdacs');

const ReferenceImportRequestDatasetEnum(this.value);

final String value;

@override
String toString() => value;
}


