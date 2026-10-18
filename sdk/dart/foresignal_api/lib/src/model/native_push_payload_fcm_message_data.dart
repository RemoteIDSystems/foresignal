//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_fcm_message_data.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadFcmMessageData {
  /// Returns a new [NativePushPayloadFcmMessageData] instance.
  NativePushPayloadFcmMessageData({

     this.v,

     this.id,

     this.a,

     this.s,

     this.ev,

     this.l,

     this.t,

     this.c,
  });

  @JsonKey(
    
    name: r'v',
    required: false,
    includeIfNull: false,
  )


  final String? v;



  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'a',
    required: false,
    includeIfNull: false,
  )


  final String? a;



  @JsonKey(
    
    name: r's',
    required: false,
    includeIfNull: false,
  )


  final String? s;



  @JsonKey(
    
    name: r'ev',
    required: false,
    includeIfNull: false,
  )


  final String? ev;



  @JsonKey(
    
    name: r'l',
    required: false,
    includeIfNull: false,
  )


  final String? l;



  @JsonKey(
    
    name: r't',
    required: false,
    includeIfNull: false,
  )


  final String? t;



      /// Category FORESIGNAL_<EVENT> (032; actionable categories arrive in 033)
  @JsonKey(
    
    name: r'c',
    required: false,
    includeIfNull: false,
  )


  final String? c;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadFcmMessageData &&
      other.v == v &&
      other.id == id &&
      other.a == a &&
      other.s == s &&
      other.ev == ev &&
      other.l == l &&
      other.t == t &&
      other.c == c;

    @override
    int get hashCode =>
        v.hashCode +
        id.hashCode +
        a.hashCode +
        s.hashCode +
        ev.hashCode +
        l.hashCode +
        t.hashCode +
        c.hashCode;

  factory NativePushPayloadFcmMessageData.fromJson(Map<String, dynamic> json) => _$NativePushPayloadFcmMessageDataFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadFcmMessageDataToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

