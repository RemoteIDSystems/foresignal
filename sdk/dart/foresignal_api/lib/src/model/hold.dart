//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hold.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Hold {
  /// Returns a new [Hold] instance.
  Hold({

    required  this.id,

    required  this.name,

    required  this.reason,

     this.sources,

     this.kinds,

     this.from,

     this.to,

     this.fromSeq,

     this.toSeq,

     this.expiresAt,

    required  this.createdAt,

     this.createdBy,

    required  this.heldRows,

    required  this.heldBytes,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



  @JsonKey(
    
    name: r'heldRows',
    required: true,
    includeIfNull: false,
  )


  final int heldRows;



  @JsonKey(
    
    name: r'heldBytes',
    required: true,
    includeIfNull: false,
  )


  final int heldBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Hold &&
      other.id == id &&
      other.name == name &&
      other.reason == reason &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.from == from &&
      other.to == to &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.expiresAt == expiresAt &&
      other.createdAt == createdAt &&
      other.createdBy == createdBy &&
      other.heldRows == heldRows &&
      other.heldBytes == heldBytes;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        reason.hashCode +
        sources.hashCode +
        kinds.hashCode +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        (fromSeq == null ? 0 : fromSeq.hashCode) +
        (toSeq == null ? 0 : toSeq.hashCode) +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        createdAt.hashCode +
        createdBy.hashCode +
        heldRows.hashCode +
        heldBytes.hashCode;

  factory Hold.fromJson(Map<String, dynamic> json) => _$HoldFromJson(json);

  Map<String, dynamic> toJson() => _$HoldToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

