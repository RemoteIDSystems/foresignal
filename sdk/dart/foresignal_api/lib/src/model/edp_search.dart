//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_search.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSearch {
  /// Returns a new [EdpSearch] instance.
  EdpSearch({

    required  this.url,

     this.auth,

     this.header,
  });

  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



  @JsonKey(
    
    name: r'auth',
    required: false,
    includeIfNull: false,
  )


  final EdpSearchAuthEnum? auth;



  @JsonKey(
    
    name: r'header',
    required: false,
    includeIfNull: false,
  )


  final String? header;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSearch &&
      other.url == url &&
      other.auth == auth &&
      other.header == header;

    @override
    int get hashCode =>
        url.hashCode +
        auth.hashCode +
        (header == null ? 0 : header.hashCode);

  factory EdpSearch.fromJson(Map<String, dynamic> json) => _$EdpSearchFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSearchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpSearchAuthEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'bearer')
bearer(r'bearer'),
@JsonValue(r'header')
header(r'header'),
@JsonValue(r'basic')
basic(r'basic');

const EdpSearchAuthEnum(this.value);

final String value;

@override
String toString() => value;
}


