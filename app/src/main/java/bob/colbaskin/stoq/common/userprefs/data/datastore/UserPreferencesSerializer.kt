package bob.colbaskin.stoq.common.userprefs.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import bob.colbaskin.stoq.datastore.UserPreferencesProto
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesSerializer : Serializer<UserPreferencesProto> {
    override val defaultValue: UserPreferencesProto = UserPreferencesProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPreferencesProto = try {
        UserPreferencesProto.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read user preferences", exception)
    }

    override suspend fun writeTo(t: UserPreferencesProto, output: OutputStream) {
        t.writeTo(output)
    }
}
