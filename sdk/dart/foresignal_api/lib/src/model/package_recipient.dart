//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'package_recipient.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackageRecipient {
  /// Returns a new [PackageRecipient] instance.
  PackageRecipient({

    required  this.instanceId,

    required  this.principals,
  });

  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



      /// Principal handles on that instance; `*` allowed only for GREEN/CLEAR
  @JsonKey(
    
    name: r'principals',
    required: true,
    includeIfNull: false,
  )


  final List<String> principals;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackageRecipient &&
      other.instanceId == instanceId &&
      other.principals == principals;

    @override
    int get hashCode =>
        instanceId.hashCode +
        principals.hashCode;

  factory PackageRecipient.fromJson(Map<String, dynamic> json) => _$PackageRecipientFromJson(json);

  Map<String, dynamic> toJson() => _$PackageRecipientToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

