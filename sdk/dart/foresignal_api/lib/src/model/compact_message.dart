//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'compact_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CompactMessage {
  /// Returns a new [CompactMessage] instance.
  CompactMessage({

    required  this.n0,

    required  this.n1,

     this.n2,

    required  this.n3,

     this.n4,

     this.n5,

     this.n6,

    required  this.n7,

     this.n8,

     this.n9,

     this.n10,

     this.n11,

     this.n12,
  });

      /// Format version
  @JsonKey(
    
    name: r'0',
    required: true,
    includeIfNull: false,
  )


  final CompactMessageN0Enum n0;



      /// Message type (084 types; 5 = intelligence event)
          // minimum: 1
  @JsonKey(
    
    name: r'1',
    required: true,
    includeIfNull: false,
  )


  final int n1;



      /// Short code
  @JsonKey(
    
    name: r'2',
    required: false,
    includeIfNull: false,
  )


  final String? n2;



      /// Severity: 0 info, 1 warning, 2 critical
          // minimum: 0
          // maximum: 2
  @JsonKey(
    
    name: r'3',
    required: true,
    includeIfNull: false,
  )


  final int n3;



      /// Kind
  @JsonKey(
    
    name: r'4',
    required: false,
    includeIfNull: false,
  )


  final String? n4;



      /// Latitude × 1e5
  @JsonKey(
    
    name: r'5',
    required: false,
    includeIfNull: false,
  )


  final int? n5;



      /// Longitude × 1e5
  @JsonKey(
    
    name: r'6',
    required: false,
    includeIfNull: false,
  )


  final int? n6;



      /// Epoch seconds
  @JsonKey(
    
    name: r'7',
    required: true,
    includeIfNull: false,
  )


  final int n7;



      /// Title (truncated on a UTF-8 boundary)
  @JsonKey(
    
    name: r'8',
    required: false,
    includeIfNull: false,
  )


  final String? n8;



      /// Source id
  @JsonKey(
    
    name: r'9',
    required: false,
    includeIfNull: false,
  )


  final String? n9;



      /// Origin instance short id (4 bytes)
  @JsonKey(
    
    name: r'10',
    required: false,
    includeIfNull: false,
  )


  final String? n10;



      /// Message id: first 8 bytes of SHA-256 of the origin ref
  @JsonKey(
    
    name: r'11',
    required: false,
    includeIfNull: false,
  )


  final String? n11;



      /// Expiry in minutes after key 7
          // minimum: 0
  @JsonKey(
    
    name: r'12',
    required: false,
    includeIfNull: false,
  )


  final int? n12;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CompactMessage &&
      other.n0 == n0 &&
      other.n1 == n1 &&
      other.n2 == n2 &&
      other.n3 == n3 &&
      other.n4 == n4 &&
      other.n5 == n5 &&
      other.n6 == n6 &&
      other.n7 == n7 &&
      other.n8 == n8 &&
      other.n9 == n9 &&
      other.n10 == n10 &&
      other.n11 == n11 &&
      other.n12 == n12;

    @override
    int get hashCode =>
        n0.hashCode +
        n1.hashCode +
        n2.hashCode +
        n3.hashCode +
        n4.hashCode +
        n5.hashCode +
        n6.hashCode +
        n7.hashCode +
        n8.hashCode +
        n9.hashCode +
        n10.hashCode +
        n11.hashCode +
        n12.hashCode;

  factory CompactMessage.fromJson(Map<String, dynamic> json) => _$CompactMessageFromJson(json);

  Map<String, dynamic> toJson() => _$CompactMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Format version
enum CompactMessageN0Enum {
@JsonValue(1)
number1('1');

const CompactMessageN0Enum(this.value);

final String value;

@override
String toString() => value;
}


