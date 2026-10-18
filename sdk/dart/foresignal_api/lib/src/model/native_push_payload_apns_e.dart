//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_apns_e.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadApnsE {
  /// Returns a new [NativePushPayloadApnsE] instance.
  NativePushPayloadApnsE({

    required  this.v,

    required  this.id,

     this.a,

    required  this.s,

    required  this.ev,

     this.l,
  });

  @JsonKey(
    
    name: r'v',
    required: true,
    includeIfNull: false,
  )


  final NativePushPayloadApnsEVEnum v;



      /// The notification id (0 for a test push)
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'a',
    required: false,
    includeIfNull: false,
  )


  final int? a;



  @JsonKey(
    
    name: r's',
    required: true,
    includeIfNull: false,
  )


  final NativePushPayloadApnsESEnum s;



  @JsonKey(
    
    name: r'ev',
    required: true,
    includeIfNull: false,
  )


  final NativePushPayloadApnsEEvEnum ev;



  @JsonKey(
    
    name: r'l',
    required: false,
    includeIfNull: false,
  )


  final String? l;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadApnsE &&
      other.v == v &&
      other.id == id &&
      other.a == a &&
      other.s == s &&
      other.ev == ev &&
      other.l == l;

    @override
    int get hashCode =>
        v.hashCode +
        id.hashCode +
        (a == null ? 0 : a.hashCode) +
        s.hashCode +
        ev.hashCode +
        l.hashCode;

  factory NativePushPayloadApnsE.fromJson(Map<String, dynamic> json) => _$NativePushPayloadApnsEFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadApnsEToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NativePushPayloadApnsEVEnum {
@JsonValue(1)
number1('1');

const NativePushPayloadApnsEVEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NativePushPayloadApnsESEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const NativePushPayloadApnsESEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NativePushPayloadApnsEEvEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system');

const NativePushPayloadApnsEEvEnum(this.value);

final String value;

@override
String toString() => value;
}


