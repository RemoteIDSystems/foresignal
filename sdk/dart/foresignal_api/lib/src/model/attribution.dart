//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attribution.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Attribution {
  /// Returns a new [Attribution] instance.
  Attribution({

    required  this.text,

     this.html,

     this.url,

    required  this.licence,

     this.licenceUrl,

    required  this.requiredDisplay,

     this.sourceId,

     this.termsUrl,

     this.count,
  });

      /// Plain-text attribution line
  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



      /// HTML form of the attribution
  @JsonKey(
    
    name: r'html',
    required: false,
    includeIfNull: false,
  )


  final String? html;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



      /// Licence identifier
  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'licenceUrl',
    required: false,
    includeIfNull: false,
  )


  final String? licenceUrl;



      /// True when the licence requires the attribution to be visible on the map or image
  @JsonKey(
    
    name: r'requiredDisplay',
    required: true,
    includeIfNull: false,
  )


  final bool requiredDisplay;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'termsUrl',
    required: false,
    includeIfNull: false,
  )


  final String? termsUrl;



      /// Records in the response from this source under this licence
          // minimum: 0
  @JsonKey(
    
    name: r'count',
    required: false,
    includeIfNull: false,
  )


  final int? count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Attribution &&
      other.text == text &&
      other.html == html &&
      other.url == url &&
      other.licence == licence &&
      other.licenceUrl == licenceUrl &&
      other.requiredDisplay == requiredDisplay &&
      other.sourceId == sourceId &&
      other.termsUrl == termsUrl &&
      other.count == count;

    @override
    int get hashCode =>
        text.hashCode +
        (html == null ? 0 : html.hashCode) +
        (url == null ? 0 : url.hashCode) +
        licence.hashCode +
        (licenceUrl == null ? 0 : licenceUrl.hashCode) +
        requiredDisplay.hashCode +
        sourceId.hashCode +
        (termsUrl == null ? 0 : termsUrl.hashCode) +
        count.hashCode;

  factory Attribution.fromJson(Map<String, dynamic> json) => _$AttributionFromJson(json);

  Map<String, dynamic> toJson() => _$AttributionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

