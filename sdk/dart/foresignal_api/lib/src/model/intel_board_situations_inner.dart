//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_situations_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardSituationsInner {
  /// Returns a new [IntelBoardSituationsInner] instance.
  IntelBoardSituationsInner({

    required  this.id,

    required  this.title,

     this.status,

    required  this.severity,

     this.lastActivityAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final String? status;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final Severity severity;



  @JsonKey(
    
    name: r'lastActivityAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastActivityAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardSituationsInner &&
      other.id == id &&
      other.title == title &&
      other.status == status &&
      other.severity == severity &&
      other.lastActivityAt == lastActivityAt;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        status.hashCode +
        severity.hashCode +
        lastActivityAt.hashCode;

  factory IntelBoardSituationsInner.fromJson(Map<String, dynamic> json) => _$IntelBoardSituationsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardSituationsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

