//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_source_info_access.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSourceInfoAccess {
  /// Returns a new [EdpSourceInfoAccess] instance.
  EdpSourceInfoAccess({

    required  this.route,

     this.fragility,
  });

  @JsonKey(
    
    name: r'route',
    required: true,
    includeIfNull: false,
  )


  final EdpSourceInfoAccessRouteEnum route;



  @JsonKey(
    
    name: r'fragility',
    required: false,
    includeIfNull: false,
  )


  final EdpSourceInfoAccessFragilityEnum? fragility;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSourceInfoAccess &&
      other.route == route &&
      other.fragility == fragility;

    @override
    int get hashCode =>
        route.hashCode +
        fragility.hashCode;

  factory EdpSourceInfoAccess.fromJson(Map<String, dynamic> json) => _$EdpSourceInfoAccessFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSourceInfoAccessToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpSourceInfoAccessRouteEnum {
@JsonValue(r'official')
official(r'official'),
@JsonValue(r'unofficial-api')
unofficialApi(r'unofficial-api'),
@JsonValue(r'scrape')
scrape(r'scrape'),
@JsonValue(r'browser')
browser(r'browser'),
@JsonValue(r'other')
other(r'other');

const EdpSourceInfoAccessRouteEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpSourceInfoAccessFragilityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const EdpSourceInfoAccessFragilityEnum(this.value);

final String value;

@override
String toString() => value;
}


