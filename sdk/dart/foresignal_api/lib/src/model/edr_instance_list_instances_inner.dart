//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edr_instance_list_instances_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdrInstanceListInstancesInner {
  /// Returns a new [EdrInstanceListInstancesInner] instance.
  EdrInstanceListInstancesInner({

    required  this.id,

     this.title,

     this.extent,

    required  this.links,
  });

      /// Run time, ISO extended
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'extent',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? extent;



  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdrInstanceListInstancesInner &&
      other.id == id &&
      other.title == title &&
      other.extent == extent &&
      other.links == links;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        extent.hashCode +
        links.hashCode;

  factory EdrInstanceListInstancesInner.fromJson(Map<String, dynamic> json) => _$EdrInstanceListInstancesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EdrInstanceListInstancesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

