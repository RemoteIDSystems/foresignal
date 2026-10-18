//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/edr_instance_list_instances_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edr_instance_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdrInstanceList {
  /// Returns a new [EdrInstanceList] instance.
  EdrInstanceList({

    required  this.instances,

     this.links,
  });

  @JsonKey(
    
    name: r'instances',
    required: true,
    includeIfNull: false,
  )


  final List<EdrInstanceListInstancesInner> instances;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final List<OgcLink>? links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdrInstanceList &&
      other.instances == instances &&
      other.links == links;

    @override
    int get hashCode =>
        instances.hashCode +
        links.hashCode;

  factory EdrInstanceList.fromJson(Map<String, dynamic> json) => _$EdrInstanceListFromJson(json);

  Map<String, dynamic> toJson() => _$EdrInstanceListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

