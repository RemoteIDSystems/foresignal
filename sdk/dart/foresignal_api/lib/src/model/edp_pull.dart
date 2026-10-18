//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_pull.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpPull {
  /// Returns a new [EdpPull] instance.
  EdpPull({

    required  this.itemsUrl,

     this.intervalSeconds = 300,

     this.auth,

     this.header,
  });

  @JsonKey(
    
    name: r'itemsUrl',
    required: true,
    includeIfNull: false,
  )


  final String itemsUrl;



          // minimum: 60
  @JsonKey(
    defaultValue: 300,
    name: r'intervalSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? intervalSeconds;



  @JsonKey(
    
    name: r'auth',
    required: false,
    includeIfNull: false,
  )


  final EdpPullAuthEnum? auth;



  @JsonKey(
    
    name: r'header',
    required: false,
    includeIfNull: false,
  )


  final String? header;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpPull &&
      other.itemsUrl == itemsUrl &&
      other.intervalSeconds == intervalSeconds &&
      other.auth == auth &&
      other.header == header;

    @override
    int get hashCode =>
        itemsUrl.hashCode +
        intervalSeconds.hashCode +
        auth.hashCode +
        (header == null ? 0 : header.hashCode);

  factory EdpPull.fromJson(Map<String, dynamic> json) => _$EdpPullFromJson(json);

  Map<String, dynamic> toJson() => _$EdpPullToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpPullAuthEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'bearer')
bearer(r'bearer'),
@JsonValue(r'header')
header(r'header'),
@JsonValue(r'basic')
basic(r'basic');

const EdpPullAuthEnum(this.value);

final String value;

@override
String toString() => value;
}


