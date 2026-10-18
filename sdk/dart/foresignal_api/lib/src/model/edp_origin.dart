//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_origin.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpOrigin {
  /// Returns a new [EdpOrigin] instance.
  EdpOrigin({

    required  this.originator,

     this.class_,

     this.upstreamId,

     this.derivedFrom,
  });

  @JsonKey(
    
    name: r'originator',
    required: true,
    includeIfNull: false,
  )


  final String originator;



  @JsonKey(
    
    name: r'class',
    required: false,
    includeIfNull: false,
  )


  final EdpOriginClass_Enum? class_;



      /// The original publisher's id for the observation or report
  @JsonKey(
    
    name: r'upstreamId',
    required: false,
    includeIfNull: false,
  )


  final String? upstreamId;



      /// Other items (stream:id) or upstream ids this item was derived from
  @JsonKey(
    
    name: r'derivedFrom',
    required: false,
    includeIfNull: false,
  )


  final List<String>? derivedFrom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpOrigin &&
      other.originator == originator &&
      other.class_ == class_ &&
      other.upstreamId == upstreamId &&
      other.derivedFrom == derivedFrom;

    @override
    int get hashCode =>
        originator.hashCode +
        (class_ == null ? 0 : class_.hashCode) +
        (upstreamId == null ? 0 : upstreamId.hashCode) +
        derivedFrom.hashCode;

  factory EdpOrigin.fromJson(Map<String, dynamic> json) => _$EdpOriginFromJson(json);

  Map<String, dynamic> toJson() => _$EdpOriginToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpOriginClass_Enum {
@JsonValue(r'sensor-network')
sensorNetwork(r'sensor-network'),
@JsonValue(r'agency')
agency(r'agency'),
@JsonValue(r'aggregator')
aggregator(r'aggregator'),
@JsonValue(r'media')
media(r'media'),
@JsonValue(r'crowd')
crowd(r'crowd'),
@JsonValue(r'social')
social(r'social'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'derived')
derived(r'derived');

const EdpOriginClass_Enum(this.value);

final String value;

@override
String toString() => value;
}


