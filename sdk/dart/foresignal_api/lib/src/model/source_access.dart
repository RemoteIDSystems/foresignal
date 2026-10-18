//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_access.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceAccess {
  /// Returns a new [SourceAccess] instance.
  SourceAccess({

    required  this.route,

     this.termsSummary,

     this.termsUrl,

     this.fragility,
  });

  @JsonKey(
    
    name: r'route',
    required: true,
    includeIfNull: false,
  )


  final SourceAccessRouteEnum route;



  @JsonKey(
    
    name: r'termsSummary',
    required: false,
    includeIfNull: false,
  )


  final String? termsSummary;



  @JsonKey(
    
    name: r'termsUrl',
    required: false,
    includeIfNull: false,
  )


  final String? termsUrl;



  @JsonKey(
    
    name: r'fragility',
    required: false,
    includeIfNull: false,
  )


  final SourceAccessFragilityEnum? fragility;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceAccess &&
      other.route == route &&
      other.termsSummary == termsSummary &&
      other.termsUrl == termsUrl &&
      other.fragility == fragility;

    @override
    int get hashCode =>
        route.hashCode +
        (termsSummary == null ? 0 : termsSummary.hashCode) +
        (termsUrl == null ? 0 : termsUrl.hashCode) +
        fragility.hashCode;

  factory SourceAccess.fromJson(Map<String, dynamic> json) => _$SourceAccessFromJson(json);

  Map<String, dynamic> toJson() => _$SourceAccessToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SourceAccessRouteEnum {
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

const SourceAccessRouteEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SourceAccessFragilityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const SourceAccessFragilityEnum(this.value);

final String value;

@override
String toString() => value;
}


