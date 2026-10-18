//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'population_pack_import_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PopulationPackImportRequest {
  /// Returns a new [PopulationPackImportRequest] instance.
  PopulationPackImportRequest({

    required  this.path,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PopulationPackImportRequest &&
      other.path == path;

    @override
    int get hashCode =>
        path.hashCode;

  factory PopulationPackImportRequest.fromJson(Map<String, dynamic> json) => _$PopulationPackImportRequestFromJson(json);

  Map<String, dynamic> toJson() => _$PopulationPackImportRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

