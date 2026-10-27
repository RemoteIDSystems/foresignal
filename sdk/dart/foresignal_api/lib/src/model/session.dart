//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'session.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Session {
  /// Returns a new [Session] instance.
  Session({

    required  this.id,

     this.tokenPrefix,

    required  this.createdAt,

     this.lastSeenAt,

    required  this.expiresAt,

     this.idleExpiresAt,

     this.userAgent,

     this.ipAddress,

    required  this.current,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'tokenPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? tokenPrefix;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastSeenAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSeenAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'idleExpiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? idleExpiresAt;



  @JsonKey(
    
    name: r'userAgent',
    required: false,
    includeIfNull: false,
  )


  final String? userAgent;



      /// Source IP at creation
  @JsonKey(
    
    name: r'ipAddress',
    required: false,
    includeIfNull: false,
  )


  final String? ipAddress;



      /// True for the session making this request
  @JsonKey(
    
    name: r'current',
    required: true,
    includeIfNull: false,
  )


  final bool current;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Session &&
      other.id == id &&
      other.tokenPrefix == tokenPrefix &&
      other.createdAt == createdAt &&
      other.lastSeenAt == lastSeenAt &&
      other.expiresAt == expiresAt &&
      other.idleExpiresAt == idleExpiresAt &&
      other.userAgent == userAgent &&
      other.ipAddress == ipAddress &&
      other.current == current;

    @override
    int get hashCode =>
        id.hashCode +
        tokenPrefix.hashCode +
        createdAt.hashCode +
        (lastSeenAt == null ? 0 : lastSeenAt.hashCode) +
        expiresAt.hashCode +
        (idleExpiresAt == null ? 0 : idleExpiresAt.hashCode) +
        (userAgent == null ? 0 : userAgent.hashCode) +
        (ipAddress == null ? 0 : ipAddress.hashCode) +
        current.hashCode;

  factory Session.fromJson(Map<String, dynamic> json) => _$SessionFromJson(json);

  Map<String, dynamic> toJson() => _$SessionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

