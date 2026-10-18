//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'detector_ref.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DetectorRef {
  /// Returns a new [DetectorRef] instance.
  DetectorRef({

    required  this.name,

    required  this.version,

     this.params,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DetectorRef &&
      other.name == name &&
      other.version == version &&
      other.params == params;

    @override
    int get hashCode =>
        name.hashCode +
        version.hashCode +
        params.hashCode;

  factory DetectorRef.fromJson(Map<String, dynamic> json) => _$DetectorRefFromJson(json);

  Map<String, dynamic> toJson() => _$DetectorRefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

