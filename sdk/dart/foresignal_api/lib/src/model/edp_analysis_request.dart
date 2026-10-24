//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_analysis_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpAnalysisRequest {
  /// Returns a new [EdpAnalysisRequest] instance.
  EdpAnalysisRequest({

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


  final EdpAnalysisRequestAuthEnum? auth;



  @JsonKey(
    
    name: r'header',
    required: false,
    includeIfNull: false,
  )


  final String? header;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpAnalysisRequest &&
      other.url == url &&
      other.auth == auth &&
      other.header == header;

    @override
    int get hashCode =>
        url.hashCode +
        auth.hashCode +
        (header == null ? 0 : header.hashCode);

  factory EdpAnalysisRequest.fromJson(Map<String, dynamic> json) => _$EdpAnalysisRequestFromJson(json);

  Map<String, dynamic> toJson() => _$EdpAnalysisRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpAnalysisRequestAuthEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'bearer')
bearer(r'bearer'),
@JsonValue(r'header')
header(r'header'),
@JsonValue(r'basic')
basic(r'basic');

const EdpAnalysisRequestAuthEnum(this.value);

final String value;

@override
String toString() => value;
}


