//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcLink {
  /// Returns a new [OgcLink] instance.
  OgcLink({

    required  this.href,

    required  this.rel,

     this.type,

     this.title,

     this.templated,
  });

  @JsonKey(
    
    name: r'href',
    required: true,
    includeIfNull: false,
  )


  final String href;



  @JsonKey(
    
    name: r'rel',
    required: true,
    includeIfNull: false,
  )


  final String rel;



  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final String? type;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'templated',
    required: false,
    includeIfNull: false,
  )


  final bool? templated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcLink &&
      other.href == href &&
      other.rel == rel &&
      other.type == type &&
      other.title == title &&
      other.templated == templated;

    @override
    int get hashCode =>
        href.hashCode +
        rel.hashCode +
        type.hashCode +
        title.hashCode +
        templated.hashCode;

  factory OgcLink.fromJson(Map<String, dynamic> json) => _$OgcLinkFromJson(json);

  Map<String, dynamic> toJson() => _$OgcLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

