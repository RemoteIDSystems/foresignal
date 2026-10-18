//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_source_info_terms.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSourceInfoTerms {
  /// Returns a new [EdpSourceInfoTerms] instance.
  EdpSourceInfoTerms({

     this.summary,

     this.url,
  });

  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSourceInfoTerms &&
      other.summary == summary &&
      other.url == url;

    @override
    int get hashCode =>
        (summary == null ? 0 : summary.hashCode) +
        (url == null ? 0 : url.hashCode);

  factory EdpSourceInfoTerms.fromJson(Map<String, dynamic> json) => _$EdpSourceInfoTermsFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSourceInfoTermsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

