//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'principal_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrincipalSummary {
  /// Returns a new [PrincipalSummary] instance.
  PrincipalSummary({

    required  this.id,

    required  this.handle,

    required  this.displayName,

    required  this.kind,

     this.avatarMediaId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'handle',
    required: true,
    includeIfNull: false,
  )


  final String handle;



  @JsonKey(
    
    name: r'displayName',
    required: true,
    includeIfNull: false,
  )


  final String displayName;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummaryKindEnum kind;



      /// 043 media id of the avatar image
  @JsonKey(
    
    name: r'avatarMediaId',
    required: false,
    includeIfNull: false,
  )


  final String? avatarMediaId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrincipalSummary &&
      other.id == id &&
      other.handle == handle &&
      other.displayName == displayName &&
      other.kind == kind &&
      other.avatarMediaId == avatarMediaId;

    @override
    int get hashCode =>
        id.hashCode +
        handle.hashCode +
        displayName.hashCode +
        kind.hashCode +
        (avatarMediaId == null ? 0 : avatarMediaId.hashCode);

  factory PrincipalSummary.fromJson(Map<String, dynamic> json) => _$PrincipalSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$PrincipalSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrincipalSummaryKindEnum {
@JsonValue(r'person')
person(r'person'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'service')
service(r'service');

const PrincipalSummaryKindEnum(this.value);

final String value;

@override
String toString() => value;
}


