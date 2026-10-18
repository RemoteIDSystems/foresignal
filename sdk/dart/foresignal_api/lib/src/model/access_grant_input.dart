//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'access_grant_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AccessGrantInput {
  /// Returns a new [AccessGrantInput] instance.
  AccessGrantInput({

     this.role,

     this.expiresAt,

     this.deliverAs,

     this.message,
  });

  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final AccessGrantInputRoleEnum? role;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



      /// For remote requesters: a 132 package
  @JsonKey(
    
    name: r'deliverAs',
    required: false,
    includeIfNull: false,
  )


  final AccessGrantInputDeliverAsEnum? deliverAs;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AccessGrantInput &&
      other.role == role &&
      other.expiresAt == expiresAt &&
      other.deliverAs == deliverAs &&
      other.message == message;

    @override
    int get hashCode =>
        role.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        deliverAs.hashCode +
        (message == null ? 0 : message.hashCode);

  factory AccessGrantInput.fromJson(Map<String, dynamic> json) => _$AccessGrantInputFromJson(json);

  Map<String, dynamic> toJson() => _$AccessGrantInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AccessGrantInputRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter');

const AccessGrantInputRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


/// For remote requesters: a 132 package
enum AccessGrantInputDeliverAsEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'chronicle-extract')
chronicleExtract(r'chronicle-extract');

const AccessGrantInputDeliverAsEnum(this.value);

final String value;

@override
String toString() => value;
}


