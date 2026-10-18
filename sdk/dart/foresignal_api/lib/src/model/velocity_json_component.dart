//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/velocity_json_component_header.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'velocity_json_component.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class VelocityJsonComponent {
  /// Returns a new [VelocityJsonComponent] instance.
  VelocityJsonComponent({

    required  this.header,

    required  this.data,
  });

  @JsonKey(
    
    name: r'header',
    required: true,
    includeIfNull: false,
  )


  final VelocityJsonComponentHeader header;



  @JsonKey(
    
    name: r'data',
    required: true,
    includeIfNull: false,
  )


  final List<num?> data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is VelocityJsonComponent &&
      other.header == header &&
      other.data == data;

    @override
    int get hashCode =>
        header.hashCode +
        data.hashCode;

  factory VelocityJsonComponent.fromJson(Map<String, dynamic> json) => _$VelocityJsonComponentFromJson(json);

  Map<String, dynamic> toJson() => _$VelocityJsonComponentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

