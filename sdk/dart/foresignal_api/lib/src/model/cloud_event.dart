//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cloud_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CloudEvent {
  /// Returns a new [CloudEvent] instance.
  CloudEvent({

    required  this.specversion,

    required  this.id,

    required  this.source_,

    required  this.type,

     this.subject,

     this.time,

     this.datacontenttype,

     this.dataschema,

     this.data,
  });

  @JsonKey(
    
    name: r'specversion',
    required: true,
    includeIfNull: false,
  )


  final CloudEventSpecversionEnum specversion;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'subject',
    required: false,
    includeIfNull: false,
  )


  final String? subject;



  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final DateTime? time;



  @JsonKey(
    
    name: r'datacontenttype',
    required: false,
    includeIfNull: false,
  )


  final String? datacontenttype;



  @JsonKey(
    
    name: r'dataschema',
    required: false,
    includeIfNull: false,
  )


  final String? dataschema;



      /// Payload; an object becomes the record payload (with `geometry` or `lat`/`lon`), other values are wrapped as `{data}`
  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Object? data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CloudEvent &&
      other.specversion == specversion &&
      other.id == id &&
      other.source_ == source_ &&
      other.type == type &&
      other.subject == subject &&
      other.time == time &&
      other.datacontenttype == datacontenttype &&
      other.dataschema == dataschema &&
      other.data == data;

    @override
    int get hashCode =>
        specversion.hashCode +
        id.hashCode +
        source_.hashCode +
        type.hashCode +
        subject.hashCode +
        time.hashCode +
        datacontenttype.hashCode +
        dataschema.hashCode +
        (data == null ? 0 : data.hashCode);

  factory CloudEvent.fromJson(Map<String, dynamic> json) => _$CloudEventFromJson(json);

  Map<String, dynamic> toJson() => _$CloudEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CloudEventSpecversionEnum {
@JsonValue(r'1.0')
n1period0(r'1.0');

const CloudEventSpecversionEnum(this.value);

final String value;

@override
String toString() => value;
}


